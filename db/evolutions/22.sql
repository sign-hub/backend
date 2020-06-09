#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    alter table Feature 
        add column active boolean;

-- schema update # 2

    alter table Feature 
        add column area_uuid varchar(255);

-- schema update # 3

    create table FeatureArea (
        uuid varchar(255) not null,
        areaDescription varchar(255),
        name varchar(255),
        primary key (uuid)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- schema update # 4

    alter table Feature 
        add index FK_5v0rhsqc7ej2spl0myom17cqx (area_uuid), 
        add constraint FK_5v0rhsqc7ej2spl0myom17cqx 
        foreign key (area_uuid) 
        references FeatureArea (uuid);