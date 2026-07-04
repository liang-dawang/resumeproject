package org.example.newcarre.dto;


import lombok.Data;

import java.util.List;

@Data
public class JobCategoryProfile {
    private String jobCategory;
    private List<String> jobDuty;

}
