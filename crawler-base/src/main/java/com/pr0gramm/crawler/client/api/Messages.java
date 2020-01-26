package com.pr0gramm.crawler.client.api;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Messages {

    private List<Message> messages = new ArrayList<>();

}
