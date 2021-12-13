package com.example.fileloader.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Table(name = "FileDatas")
public class FileData extends BaseModel {

    public static final int MAX_SIZE = 10000000;

    @Lob
    private byte data[];

    public FileData(String id) {
        super();
        setId(id);
    }
}
