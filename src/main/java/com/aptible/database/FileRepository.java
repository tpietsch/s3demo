package com.aptible.database;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;

@Repository
public interface FileRepository extends CrudRepository<FileEntity, Integer>, Serializable {
    @Query("select file from FileEntity file where file.orgId = :orgId")
    List<FileEntity> findAllByOrgId(@Param("orgId") String orgId);

//    Also works - here for ref
//    List<FileEntity> findAllByOrgId(Integer orgId);
}