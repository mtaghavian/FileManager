package com.example.fileloader.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@MappedSuperclass
public class BaseModel implements Comparable<BaseModel> {

    @Id
    @Column
    private String id;

    @Column
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Date mdate;

    @PrePersist
    public void onPrePersist() {
        id = "" + UUID.randomUUID();
        mdate = new Date();
    }

    @PreUpdate
    public void onPreUpdate() {
        mdate = new Date();
    }

    @Override
    public int compareTo(BaseModel o) {
        return mdate.compareTo(o.mdate);
    }
}
