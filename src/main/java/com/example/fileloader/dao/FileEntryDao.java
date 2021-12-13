package com.example.fileloader.dao;

import com.example.fileloader.model.FileEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author masoud
 */
@Repository
public interface FileEntryDao extends JpaRepository<FileEntry, String> {

    public FileEntry findByName(String name);
}
