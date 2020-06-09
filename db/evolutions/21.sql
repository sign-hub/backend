#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    create table Contacts (
        uuid varchar(255) not null,
        email varchar(255),
        tool varchar(255),
        primary key (uuid)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- schema update # 2

    create table Feature (
        uuid varchar(255) not null,
        code varchar(255),
        featureDescription varchar(255),
        featureType varchar(255),
        name varchar(255),
        primary key (uuid)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- schema update # 3

    create table FeatureValue (
        uuid varchar(255) not null,
        value varchar(255),
        feature_uuid varchar(255),
        signLanguage_uuid varchar(255),
        primary key (uuid)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- schema update # 4

    create table SignLanguage (
        uuid varchar(255) not null,
        author varchar(255),
        code varchar(255),
        countries varchar(255),
        deafCulture varchar(255),
        deafEducation varchar(255),
        linguisticStudies varchar(255),
        name varchar(255),
        usersDescription varchar(255),
        primary key (uuid)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- schema update # 5

    create table feature_options (
        id varchar(255) not null,
        value varchar(255),
        name varchar(255) not null,
        primary key (id, name)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- schema update # 6

    alter table FeatureValue 
        add index FK_51mcucuvm69gawdflb9k272x5 (feature_uuid), 
        add constraint FK_51mcucuvm69gawdflb9k272x5 
        foreign key (feature_uuid) 
        references Feature (uuid);

-- schema update # 7

    alter table FeatureValue 
        add index FK_ik56q37fmx906eq3v6sie44t1 (signLanguage_uuid), 
        add constraint FK_ik56q37fmx906eq3v6sie44t1 
        foreign key (signLanguage_uuid) 
        references SignLanguage (uuid);

-- schema update # 8

    alter table feature_options 
        add index FK_thj0pqtxk8eiy3vog3ryl17a9 (id), 
        add constraint FK_thj0pqtxk8eiy3vog3ryl17a9 
        foreign key (id) 
        references Feature (uuid);