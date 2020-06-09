#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    alter table GrammarPart 
        add column grammar_uuid varchar(255);

-- schema update # 2

    alter table GrammarPart 
        add index FK_6e9l3f2wl6sbxa4caou7vbb05 (grammar_uuid), 
        add constraint FK_6e9l3f2wl6sbxa4caou7vbb05 
        foreign key (grammar_uuid) 
        references Grammar (uuid);
