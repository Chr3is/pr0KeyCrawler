package com.pr0gramm.crawler.mapping

import com.pr0gramm.crawler.client.api.*
import com.pr0gramm.crawler.config.properties.Pr0grammApiClientProperties
import com.pr0gramm.crawler.model.PostType
import com.pr0gramm.crawler.model.client.*
import ma.glasnost.orika.MapperFacade
import spock.lang.Specification
import spock.lang.Subject

class MappingsTest extends Specification {

    static final String NEW_POST_URL = 'https://localhost:8080/'

    Pr0grammApiClientProperties properties = new Pr0grammApiClientProperties(newPostsUrl: NEW_POST_URL)

    @Subject
    MapperFacade mapper = new Mappings(properties)

    def 'Post can be mapped to Pr0Post'() {
        given:
        Post post = new Post(id: 1, created: 55, image: 'someImage.png', user: 'SomeDude')

        when:
        Pr0Post pr0Post = mapper.map(post, Pr0Post)

        then:
        verifyAll(pr0Post) {
            id == post.id
            fullUrl == NEW_POST_URL + post.id
            created == post.created
            contentLink == post.image
            user == post.user
            type == PostType.IMAGE
        }
    }

    def 'Content can be mapped to Pr0Content'() {
        given:
        Post post1 = new Post(id: 1, created: 55, image: 'someImage.png', user: 'SomeDude')
        Post post2 = new Post(id: 2, created: 100, image: 'someOtherImage.jpg', user: 'SomeOtherDude')
        Content content = new Content(items: [post1, post2])

        when:
        Pr0Content pr0Content = mapper.map(content, Pr0Content)

        then:
        verifyAll(pr0Content.posts) {
            it.size() == 2
            verifyAll(it[0]) {
                id == post2.id
                fullUrl == NEW_POST_URL + post2.id
                created == post2.created
                contentLink == post2.image
                user == post2.user
                type == PostType.IMAGE
            }
            verifyAll(it[1]) {
                id == post1.id
                fullUrl == NEW_POST_URL + post1.id
                created == post1.created
                contentLink == post1.image
                user == post1.user
                type == PostType.IMAGE
            }
        }
    }

    def 'Tag can be mapped to Pr0Tag'() {
        given:
        Tag tag = new Tag(id: 1, tag: 'Tag1')

        when:
        Pr0Tag pr0Tag = mapper.map(tag, Pr0Tag)

        then:
        verifyAll(pr0Tag) {
            id == tag.id
            name == tag.tag
        }
    }

    def 'Comment can be mapped to Pr0Comment'() {
        given:
        Comment comment = new Comment(id: 3, parent: 0, content: 'Comment1', created: 452, up: 4, down: 1, name: 'SomeDude')

        when:
        Pr0Comment pr0Comment = mapper.map(comment, Pr0Comment)

        then:
        verifyAll(pr0Comment) {
            id == comment.id
            parent == comment.parent
            content == comment.content
            created == comment.created
            up == comment.up
            down == comment.down
            userName == comment.name
        }
    }

    def 'PostInfo can be mapped to Pr0PostInfo'() {
        given:
        Tag tag1 = new Tag(id: 1, tag: 'Tag1')
        Tag tag2 = new Tag(id: 2, tag: 'Tag2')
        Comment comment1 = new Comment(id: 3, parent: 0, content: 'Comment1', created: 452, up: 4, down: 1, name: 'SomeDude')
        Comment comment2 = new Comment(id: 4, parent: 3, content: 'Comment2', created: 100, up: 10, down: 2, name: 'SomeOtherDude')
        PostInfo postInfo = new PostInfo(tags: [tag1, tag2], comments: [comment1, comment2])

        when:
        Pr0PostInfo pr0PostInfo = mapper.map(postInfo, Pr0PostInfo)

        then:
        verifyAll(pr0PostInfo) {
            verifyAll(tags) {
                size() == 2
                verifyAll(it[0]) {
                    id == tag1.id
                    name == tag1.tag
                }
                verifyAll(it[1]) {
                    id == tag2.id
                    name == tag2.tag
                }
            }
            verifyAll(comments) {
                size() == 2
                verifyAll(it[0]) {
                    id == comment1.id
                    parent == comment1.parent
                    content == comment1.content
                    created == comment1.created
                    up == comment1.up
                    down == comment1.down
                    userName == comment1.name
                }
                verifyAll(it[1]) {
                    id == comment2.id
                    parent == comment2.parent
                    content == comment2.content
                    created == comment2.created
                    up == comment2.up
                    down == comment2.down
                    userName == comment2.name
                }
            }
        }
    }

    def 'Message can be mapped to Pr0Message'() {
        given:
        Message message = new Message(id: 1, itemId: 2, senderId: 3, created: 4, name: 'someDude', message: 'Hello World')

        when:
        Pr0Message pr0Message = mapper.map(message, Pr0Message)

        then:
        verifyAll(pr0Message) {
            id == message.id
            itemId == message.itemId
            senderId == message.senderId
            created == message.created
            userName == message.name
            it.message == message.message
        }
    }

    def 'Messages can be mapped to Pr0Messages'() {
        given:
        Message message1 = new Message(id: 1, itemId: 2, senderId: 3, created: 4, name: 'SomeDude', message: 'Hello World')
        Message message2 = new Message(id: 5, itemId: 6, senderId: 7, created: 8, name: 'SomeOtherDude', message: 'Waz up')
        Messages messages = new Messages(messages: [message1, message2])

        when:
        Pr0Messages pr0Messages = mapper.map(messages, Pr0Messages)

        then:
        verifyAll(pr0Messages) {
            verifyAll(getMessages()) {
                size() == 2
                verifyAll(it[0]) {
                    id == message1.id
                    itemId == message1.itemId
                    senderId == message1.senderId
                    created == message1.created
                    userName == message1.name
                    it.message == message1.message
                }
                verifyAll(it[1]) {
                    id == message2.id
                    itemId == message2.itemId
                    senderId == message2.senderId
                    created == message2.created
                    userName == message2.name
                    it.message == message2.message
                }
            }
        }
    }
}
