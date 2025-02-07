// Topics DTO
package com.bootcoding.selector.dto;

import java.util.List;

public class Topics {
    private List<String> topics;

    // Getter
    public List<String> getTopics() {
        return topics;
    }

    // Setter
    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    // No-args constructor
    public Topics() {}

    // All-args constructor
    public Topics(List<String> topics) {
        this.topics = topics;
    }
}