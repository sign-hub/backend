#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    create table Grammar (
        uuid varchar(255) not null,
        creationDate datetime,
        deleted boolean,
        grammarName varchar(255),
        grammarStatus varchar(255),
        revisionDate datetime,
        author_uuid varchar(255),
        primary key (uuid)
    );

-- schema update # 2

    create table GrammarPart (
        uuid varchar(255) not null,
        creationDate datetime,
        deleted boolean,
        elementNumber varchar(255),
        grammarPartName varchar(255),
        grammarPartOrder integer,
        grammarPartStatus varchar(255),
        grammarPartTYpe varchar(255),
        html longtext,
        revisionDate datetime,
        author_uuid varchar(255),
        parent_uuid varchar(255),
        primary key (uuid)
    );

-- schema update # 3

    create table GrammarPart_GrammarPart (
        GrammarPart_uuid varchar(255) not null,
        parts_uuid varchar(255) not null
    );

-- schema update # 4

    create table Grammar_GrammarPart (
        Grammar_uuid varchar(255) not null,
        parts_uuid varchar(255) not null
    );

-- schema update # 5

    create table grammar_contentprovideruser (
        Grammar_uuid varchar(255) not null,
        contentProviderList_uuid varchar(255) not null
    );

-- schema update # 6

    create table grammar_editoruser (
        Grammar_uuid varchar(255) not null,
        editorList_uuid varchar(255) not null
    );

-- schema update # 7

    create table grammarpart_contentprovideruser (
        GrammarPart_uuid varchar(255) not null,
        contentProviderList_uuid varchar(255) not null
    );

-- schema update # 8

    create table grammarpart_editoruser (
        GrammarPart_uuid varchar(255) not null,
        editorList_uuid varchar(255) not null
    );

-- schema update # 9

 --   alter table GrammarPart_GrammarPart 
 --      drop constraint UK_2mi7rowfqo1m7igp6auf5k29r;

-- schema update # 10

    alter table GrammarPart_GrammarPart 
        add constraint UK_2mi7rowfqo1m7igp6auf5k29r unique (parts_uuid);

-- schema update # 11

 --   alter table Grammar_GrammarPart 
  --      drop constraint UK_nedw9rackj1nrqe0sntwq91ob;

-- schema update # 12

    alter table Grammar_GrammarPart 
        add constraint UK_nedw9rackj1nrqe0sntwq91ob unique (parts_uuid);

-- schema update # 13

    alter table Grammar 
        add index FK_oe32rw4bnqwfyugnvgylcrub5 (author_uuid), 
        add constraint FK_oe32rw4bnqwfyugnvgylcrub5 
        foreign key (author_uuid) 
        references EndUser (uuid);

-- schema update # 14

    alter table GrammarPart 
        add index FK_aobrvj49wpoax93viskpvfwx (author_uuid), 
        add constraint FK_aobrvj49wpoax93viskpvfwx 
        foreign key (author_uuid) 
        references EndUser (uuid);

-- schema update # 15

    alter table GrammarPart 
        add index FK_o4a86yyqr43fyetsrpb3sa9ut (parent_uuid), 
        add constraint FK_o4a86yyqr43fyetsrpb3sa9ut 
        foreign key (parent_uuid) 
        references GrammarPart (uuid);

-- schema update # 16

    alter table GrammarPart_GrammarPart 
        add index FK_2mi7rowfqo1m7igp6auf5k29r (parts_uuid), 
        add constraint FK_2mi7rowfqo1m7igp6auf5k29r 
        foreign key (parts_uuid) 
        references GrammarPart (uuid);

-- schema update # 17

    alter table GrammarPart_GrammarPart 
        add index FK_s2jgmqsch5ya7b7pvgatxy9i0 (GrammarPart_uuid), 
        add constraint FK_s2jgmqsch5ya7b7pvgatxy9i0 
        foreign key (GrammarPart_uuid) 
        references GrammarPart (uuid);

-- schema update # 18

    alter table Grammar_GrammarPart 
        add index FK_nedw9rackj1nrqe0sntwq91ob (parts_uuid), 
        add constraint FK_nedw9rackj1nrqe0sntwq91ob 
        foreign key (parts_uuid) 
        references GrammarPart (uuid);

-- schema update # 19

    alter table Grammar_GrammarPart 
        add index FK_q01odd0dyk5vy9o9q1qaxcght (Grammar_uuid), 
        add constraint FK_q01odd0dyk5vy9o9q1qaxcght 
        foreign key (Grammar_uuid) 
        references Grammar (uuid);

-- schema update # 20

    alter table grammar_contentprovideruser 
        add index FK_sh3llllhlereu41on0hxlx1ym (contentProviderList_uuid), 
        add constraint FK_sh3llllhlereu41on0hxlx1ym 
        foreign key (contentProviderList_uuid) 
        references EndUser (uuid);

-- schema update # 21

    alter table grammar_contentprovideruser 
        add index FK_lsxmq0mppeo22cvf8svqqhhx9 (Grammar_uuid), 
        add constraint FK_lsxmq0mppeo22cvf8svqqhhx9 
        foreign key (Grammar_uuid) 
        references Grammar (uuid);

-- schema update # 22

    alter table grammar_editoruser 
        add index FK_r3say00ks09am3miru9e05uh7 (editorList_uuid), 
        add constraint FK_r3say00ks09am3miru9e05uh7 
        foreign key (editorList_uuid) 
        references EndUser (uuid);

-- schema update # 23

    alter table grammar_editoruser 
        add index FK_7y91fop0ql69dfji35sf1wdsd (Grammar_uuid), 
        add constraint FK_7y91fop0ql69dfji35sf1wdsd 
        foreign key (Grammar_uuid) 
        references Grammar (uuid);

-- schema update # 24

    alter table grammarpart_contentprovideruser 
        add index FK_4wjrdxg1f1twe361m57reketb (contentProviderList_uuid), 
        add constraint FK_4wjrdxg1f1twe361m57reketb 
        foreign key (contentProviderList_uuid) 
        references EndUser (uuid);

-- schema update # 25

    alter table grammarpart_contentprovideruser 
        add index FK_q5oadxabxyqpgfv5xowst632g (GrammarPart_uuid), 
        add constraint FK_q5oadxabxyqpgfv5xowst632g 
        foreign key (GrammarPart_uuid) 
        references GrammarPart (uuid);

-- schema update # 26

    alter table grammarpart_editoruser 
        add index FK_ju9lm5fqmv5lt9nb1gy9w9vfb (editorList_uuid), 
        add constraint FK_ju9lm5fqmv5lt9nb1gy9w9vfb 
        foreign key (editorList_uuid) 
        references EndUser (uuid);

-- schema update # 27

    alter table grammarpart_editoruser 
        add index FK_80u8tb1yiuol2mhwu2hojugia (GrammarPart_uuid), 
        add constraint FK_80u8tb1yiuol2mhwu2hojugia 
        foreign key (GrammarPart_uuid) 
        references GrammarPart (uuid);
        
# --- !Downs
