package com.pr0gramm.keycrawler.api;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Messages {

    private List<Message> messages = new ArrayList<>();

}
