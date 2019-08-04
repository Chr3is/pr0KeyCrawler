package com.pr0gramm.keycrawler.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Table("account")
public class User {

    @Id
    private Long id;

    private Long proUserId;

    private String userName;

    private String token;

    private Long chatId;

    private boolean verified = false;

    private boolean subscribed = true;

    public User(Long proUserId, String userName, String token) {
        this.proUserId = proUserId;
        this.userName = userName;
        this.token = token;
    }

}
