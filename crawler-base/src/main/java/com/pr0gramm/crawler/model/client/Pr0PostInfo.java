package com.pr0gramm.crawler.model.client;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Pr0PostInfo {

    private List<Pr0Tag> tags = new ArrayList<>();

    private List<Pr0Comment> comments = new ArrayList<>();
}
