package com.example.fileloader.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Table(name = "FileEntries")
public class FileEntry extends BaseModel {

    @Column(unique = true)
    private String name;

    @Column
    private Long length;

    @Column
    private String fileDataId;

    private Boolean isDirectory;
}
