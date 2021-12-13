package com.example.fileloader.dao;

import com.example.fileloader.model.FileData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author masoud
 */
@Repository
public interface FileDataDao extends JpaRepository<FileData, String> {

}
