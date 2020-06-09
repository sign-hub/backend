#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    create table FeatureOption (
        uuid varchar(255) not null,
        updateDate datetime,
        optionKey varchar(255),
        optionValue varchar(255),
        updatedBy_uuid varchar(255),
        feature_uuid varchar(255),
        option_sequence integer,
        primary key (uuid)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- schema update # 2

    alter table FeatureOption 
        add index FK_huhqwmrti1enadjh0a9dt4vtd (updatedBy_uuid), 
        add constraint FK_huhqwmrti1enadjh0a9dt4vtd 
        foreign key (updatedBy_uuid) 
        references EndUser (uuid);

-- schema update # 3

    alter table FeatureOption 
        add index FK_ama7l4o6ws5061jf2cwyjou6x (feature_uuid), 
        add constraint FK_ama7l4o6ws5061jf2cwyjou6x 
        foreign key (feature_uuid) 
        references Feature (uuid);