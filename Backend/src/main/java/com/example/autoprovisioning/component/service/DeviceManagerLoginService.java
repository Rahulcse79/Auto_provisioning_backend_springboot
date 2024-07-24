package com.example.autoprovisioning.component.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.autoprovisioning.component.entity.DeviceManagerLogin;
import com.example.autoprovisioning.component.helper.RequestResponse;
import com.example.autoprovisioning.component.repository.DeviceManagerLoginRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.function.Function;

@Service
public class DeviceManagerLoginService {
    
    @Autowired
    private DeviceManagerLoginRepository repository;

    private final String SECRET_KEY = "coraltelecom.services.app-3be8dd5f-c66b-4b3c-88f5-b2d36c9238dd";
    
    public String generateToken(String username) {
        return Jwts.builder()
                .setHeaderParam("alg", "HS512")
                .setHeaderParam("typ", "JWT")
                .claim("username",username) 
                .claim("authMethod", "local")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }    

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    public DeviceManagerLogin saveUserDefault() {
        DeviceManagerLogin user = new DeviceManagerLogin();
        user.setUserName("admin");
        String password = "admin";
        String hashedPassword = hashPassword(password);
        user.setPassword(hashedPassword);
        return repository.save(user);
    }

    public RequestResponse getUserByUserName(String userName, String password) {
        RequestResponse result = new RequestResponse();
        DeviceManagerLogin user = repository.findByUserName(userName);
        if (user == null  && ("admin".equals(userName) && "admin".equals(password))) {
            user = saveUserDefault();
            String token = generateToken(userName);
            result.setStatus(0);
            result.setMessage(user.getUserName() + " Token: " + token);
            result.setMessageDetail("Login api call successful.");
            return result;
        } else {
            String hashedPassword = hashPassword(password);
            String storedPassword = user.getPassword();
            if (hashedPassword.equals(storedPassword)) {
                String token = generateToken(userName);
                System.out.println(token);
                result.setStatus(0);
                result.setMessage(user.getUserName() + " Token: " + token);
                result.setMessageDetail("Login api call successful.");
                return result;
            }
        }
      
        result.setStatus(-1);
        result.setMessage("Invalid credentials.");
        result.setMessageDetail("Login api call fail.");
        return result;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null; 
        }
    }
}
