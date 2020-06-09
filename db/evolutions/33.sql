#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    create table FeatureOptionRules (
        uuid varchar(255) not null,
        updateDate datetime,
        filter longtext,
        label varchar(255),
        updatedBy_uuid varchar(255),
        feature_uuid varchar(255),
        primary key (uuid)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- schema update # 2

    alter table SignLanguage 
        add column officialCode varchar(255);

-- schema update # 3

    alter table FeatureOptionRules 
        add index FK_angoqajkjftom18rsy2s7b9kj (updatedBy_uuid), 
        add constraint FK_angoqajkjftom18rsy2s7b9kj 
        foreign key (updatedBy_uuid) 
        references EndUser (uuid);

-- schema update # 4

    alter table FeatureOptionRules 
        add index FK_jy8b1a47uvxwegrp7rw8kxaar (feature_uuid), 
        add constraint FK_jy8b1a47uvxwegrp7rw8kxaar 
        foreign key (feature_uuid) 
        references Feature (uuid);