package br.com.marcusferraz.agentecompras.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@Entity
@Table(name = "short_links", indexes = @Index(name = "idx_url_hash", columnList = "urlHash"))
@Getter
@Setter
@NoArgsConstructor
public class ShortLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "url_hash", nullable = false, length = 64)
    private String urlHash;

    public ShortLink(String url) {
        this.url = url;
        this.urlHash = generateUrlHash(url);
    }

    private String generateUrlHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encondedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encondedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating URL hash", e);
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
