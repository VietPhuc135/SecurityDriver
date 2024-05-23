package com.example.signingoogle2.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "FILE")
public class FileUpload {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "FILENAME")
    public String fileName;

    @Column(name = "FILEID")
    public String fileId;

    @Column(name = "KEYAES")
    private String keyAES;

    @Column(name = "USERID")
    private String userId;
}
