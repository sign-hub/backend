#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    create table FeatureChoiche (
        uuid varchar(255) not null,
        updateDate datetime,
        groupName varchar(255),
        label varchar(255),
        partitionName varchar(255),
        position integer,
        updatedBy_uuid varchar(255),
        feature_uuid varchar(255),
        primary key (uuid)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- schema update # 2

    alter table FeatureChoiche 
        add index FK_rgxcbdyipkcd0kxkloqw3un2y (updatedBy_uuid), 
        add constraint FK_rgxcbdyipkcd0kxkloqw3un2y 
        foreign key (updatedBy_uuid) 
        references EndUser (uuid);

-- schema update # 3

    alter table FeatureChoiche 
        add index FK_j2puj8k8tn7nbp3ksdf7iqkon (feature_uuid), 
        add constraint FK_j2puj8k8tn7nbp3ksdf7iqkon 
        foreign key (feature_uuid) 
        references Feature (uuid);