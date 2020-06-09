#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    alter table Feature 
        add column grammarPart_uuid varchar(255);

-- schema update # 2

    alter table Feature 
        add index FK_hl9ujiaq1fmwmgyauurtavusl (grammarPart_uuid), 
        add constraint FK_hl9ujiaq1fmwmgyauurtavusl 
        foreign key (grammarPart_uuid) 
        references GrammarPart (uuid);