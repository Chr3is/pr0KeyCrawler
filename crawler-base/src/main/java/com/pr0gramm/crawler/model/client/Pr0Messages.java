package com.pr0gramm.crawler.model.client;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Pr0Messages {

    private List<Pr0Message> messages = new ArrayList<>();
}
