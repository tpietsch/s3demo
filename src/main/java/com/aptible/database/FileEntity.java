package com.aptible.database;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "file")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //for files uploaded to AWS
    @Column(name = "org_id")
    private String orgId;

    @Column(name = "s3_path")
    private String s3Path;

    @Column(name = "s3_prefix")
    private String s3Prefix;

    @Column(name = "file_bucket")
    private String fileBucket;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "upload_success")
    private Boolean uploadSuccess;

    //for files uploaded locally
    @Column(name = "local_file_path")
    private String localFilePath;
}
