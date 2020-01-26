package com.pr0gramm.crawler.mapping;

import com.pr0gramm.crawler.api.model.PostType;
import com.pr0gramm.crawler.client.api.*;
import com.pr0gramm.crawler.model.client.*;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.ConfigurableMapper;

public class Mappings extends ConfigurableMapper {

    public static final Mappings INSTANCE = new Mappings();

    private Mappings() {
    }

    @Override
    protected void configure(MapperFactory factory) {
        super.configure(factory);
        mapContentToPr0grammContent(factory);
        mapPostToPr0grammPost(factory);
        mapPostInfoToPr0grammPostInfo(factory);
        mapTagToPr0grammTag(factory);
        mapCommentToPr0grammComment(factory);
        mapMessagesToPr0grammMessages(factory);
        mapMessageToPr0grammMessage(factory);
    }


    private void mapContentToPr0grammContent(MapperFactory factory) {
        factory.classMap(Content.class, Pr0Content.class)
                .field("items", "posts");
    }

    private void mapPostToPr0grammPost(MapperFactory factory) {
        factory.classMap(Post.class, Pr0Post.class)
                .field("image", "contentLink")
                .customize(new CustomMapper<>() {
                    @Override
                    public void mapAtoB(Post post, Pr0Post pr0grammPost, MappingContext mappingContext) {
                        pr0grammPost.setType(PostType.getFrom(post.getImage()));
                    }
                })
                .byDefault();
    }

    private void mapPostInfoToPr0grammPostInfo(MapperFactory factory) {
        factory.classMap(PostInfo.class, Pr0PostInfo.class)
                .byDefault();
    }

    private void mapTagToPr0grammTag(MapperFactory factory) {
        factory.classMap(Tag.class, Pr0Tag.class)
                .field("tag", "name")
                .byDefault();
    }

    private void mapCommentToPr0grammComment(MapperFactory factory) {
        factory.classMap(Comment.class, Pr0Comment.class)
                .field("parent", "parentId")
                .field("name", "userName")
                .byDefault();
    }

    private void mapMessagesToPr0grammMessages(MapperFactory factory) {
        factory.classMap(Messages.class, Pr0Messages.class)
                .byDefault();
    }

    private void mapMessageToPr0grammMessage(MapperFactory factory) {
        factory.classMap(Message.class, Pr0Message.class)
                .field("name", "userName")
                .byDefault();
    }
}
