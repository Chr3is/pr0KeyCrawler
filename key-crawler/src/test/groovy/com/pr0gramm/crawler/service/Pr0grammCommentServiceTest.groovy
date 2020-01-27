package com.pr0gramm.crawler.service

import com.pr0gramm.crawler.api.model.NewPr0Comment
import com.pr0gramm.crawler.client.Pr0grammClient
import com.pr0gramm.crawler.client.api.Post
import com.pr0gramm.crawler.config.properties.NotificationProperties
import com.pr0gramm.crawler.model.KeyResult
import com.pr0gramm.crawler.model.client.Pr0Post
import reactor.core.publisher.Mono
import reactor.util.function.Tuples
import spock.lang.Specification
import spock.lang.Subject

class Pr0grammCommentServiceTest extends Specification {

    NotificationProperties notificationProperties = new NotificationProperties()

    Pr0grammClient client = Mock()

    @Subject
    Pr0grammCommentService pr0grammCommentService = new Pr0grammCommentService(notificationProperties, client)

    def 'comments can be posted'() {
        given:
        Pr0Post post1 = new Pr0Post(id: 1, user: 'User1')
        KeyResult result1 = new KeyResult(Tuples.of(post1, ''))
        result1.keys = ['1AB2C-D3FGH-456I7-JK8LM-NOP9Q', '5BCD-1EFG-HIJK-2LMN']

        Pr0Post post2 = new Pr0Post(id: 2, user: 'User2')
        KeyResult result2 = new KeyResult(Tuples.of(post2, ''))
        result2.keys = ['1AB2C-D3FGH-456I7-JK8LM-NOP9Q']

        List<KeyResult> results = [result1, result2]

        when:
        pr0grammCommentService.sendNewComment(results).block()

        then:
        2 * client.postNewComment(_) >> Mono.empty()
    }

    def 'comment is created correctly'() {
        given:
        Pr0Post post = new Post(id: 1, user: 'User1')
        KeyResult result = new KeyResult(Tuples.of(post, ''))
        result.keys = ['1AB2C-D3FGH-456I7-JK8LM-NOP9Q', '5BCD-1EFG-HIJK-2LMN']

        when:
        NewPr0Comment comment = pr0grammCommentService.createCommentFrom(result) //TODO should not be the task of comment service

        then:
        verifyAll(comment) {
            it.post == post
            message == """Thank you ${post.user}!
Your post was crawled and the following keys were found:
${result.keys[0]}
${result.keys[1]}

These keys were send to all registered Pr0Users via Telegram.
If you want to receive game keys through telegram as well checkout the following post: https://pr0gramm.com/new/3462430
In the future you may want to use https://pr0keys.com"""
        }
    }
}
