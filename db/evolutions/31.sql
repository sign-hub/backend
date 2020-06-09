#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    alter table Grammar 
        add column languageVersion varchar(255);

-- schema update # 2

    alter table Grammar 
        add column signlanguage_uuid varchar(255);

-- schema update # 3

    alter table Grammar 
        add index FK_h34b57a45y9i7nar58uhijl03 (signlanguage_uuid), 
        add constraint FK_h34b57a45y9i7nar58uhijl03 
        foreign key (signlanguage_uuid) 
        references SignLanguage (uuid);
