package com.aptible.api;

import com.aptible.FileService;
import com.aptible.config.AppProperties;
import com.aptible.database.FileEntity;
import com.aptible.database.FileRepository;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/org/{orgId}/file")
public class FileController {
    private final FileService fileService;
    private final FileRepository fileRepository;
    private final AppProperties appProperties;

    /**
     * - Users can upload, manage, and access files
     * - Not all users are able to upload, manage, or access all files
     * - Files are stored in a cloud storage service
     * - Files are accessible to user-managed processes with the right credentials
     * - Aptible's systems can manage customer storage at all times
     */

    public FileController(FileService fileService, FileRepository fileRepository, AppProperties appProperties) {
        this.fileService = fileService;
        this.fileRepository = fileRepository;
        this.appProperties = appProperties;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    //for reference
//    @IsOrgMember
    @PreAuthorize("hasRole('ORG_' + #orgId) and hasRole('CAN_UPLOAD')")
    public @ResponseBody Mono<FileEntity> createFileMetadata(
            @PathVariable("orgId") String orgId,
            //placeholder for anything custom about file - maybe tags?...etc...
            @RequestBody Mono<Models.FileUploadInitRequest> fileUploadInitRequest) {
        FileEntity fileEntity = new FileEntity();
        fileEntity.setOrgId(orgId);
        //TODO do some fancy org selection logic - maybe supporting many s3 access keys, cloud providers,etc...
        if(appProperties.getUseS3()) {
            //TODO pull this data from users account/org config
            //TODO pull specific S3 client from org
            //a tmp bucket
            fileEntity.setFileBucket("test-60437c2c-eb11-422b-ba0a-3926fda84681");
            fileEntity.setS3Prefix("some_folder");
        }
        return Mono.just(fileRepository.save(fileEntity));
    }

    //option to add metadata as file part instead - support multi file upload vs client doing it separate requests
    //public Mono<List<String>> uploadFiles(@RequestPart("files") Flux<FilePart> files) {
    @PostMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ORG_' + #orgId) and hasRole('CAN_UPLOAD')")
    public @ResponseBody Mono<FileEntity> uploadFileForMetadata(
            @PathVariable("orgId") String orgId,
            @PathVariable("id") String id,
            @RequestPart("file") FilePart filePart) throws Exception {
        //lookup file from db -> save to s3 -> update db -> do transaction stuff
        //TODO assert belongs to org. other perms stuff - exists 404... etc...
        var file = fileRepository.findById(Integer.valueOf(id));
        return fileService.createFile(filePart, file.get());
    }

    //get file metadata
    //TODO implement paging
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ORG_' + #orgId) and hasRole('CAN_READ_FILES')")
    public @ResponseBody Mono<List<FileEntity>> getFilesForOrg(@PathVariable("orgId") String orgId) {
        return Mono.just(fileRepository.findAllByOrgId(orgId));
    }

    @GetMapping(value = "/{id}/download")
    @PreAuthorize("hasRole('ORG_' + #orgId) and hasRole('CAN_DOWNLOAD')")
    public @ResponseBody  Mono<ResponseEntity<DataBuffer>>  downloadFile(
            @PathVariable("orgId") String orgId,
            @PathVariable("id") String id) throws Exception {
        var file = fileRepository.findById(Integer.valueOf(id)).get();
        var content =  fileService.getFileBytes(file.getId());
        DataBuffer buffer = new DefaultDataBufferFactory().wrap(content);
        //TODO should save content type to respond with correct type i.e PDF vs Video...etc..
        MediaType type = null;
        if(file.getOriginalFileName().endsWith(".pdf")) {
            type = MediaType.APPLICATION_PDF;
        } else {
            type = MediaType.APPLICATION_OCTET_STREAM;
        }
        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getOriginalFileName() + "\"")
                .contentType(type)
                .contentLength(content.length)
                .body(buffer));
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasRole('ORG_' + #orgId) and hasRole('CAN_DELETE')")
    public @ResponseBody  Mono<ResponseEntity<Object>>  deleteFIle(
            @PathVariable("orgId") String orgId,
            @PathVariable("id") String id) throws Exception {
        var file = fileRepository.findById(Integer.valueOf(id)).get();
        fileService.deleteFile(file);
        return Mono.just(ResponseEntity.noContent().build());
    }

    //    TODO update....better more granular perms
}
