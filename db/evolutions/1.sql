#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    create table EndUser (
        uuid varchar(255) not null,
        deleted boolean,
        email varchar(255),
        esAccountId varchar(255) not null,
        name varchar(255),
        registrationDate datetime,
        surname varchar(255),
        verificationCode varchar(255),
        primary key (uuid)
    );

-- schema update # 2

    create table EsAccount (
        uuid varchar(255) not null,
        password varchar(255) not null,
        userGroup varchar(255),
        username varchar(255) not null,
        primary key (uuid)
    );

-- schema update # 3

    create table EsAccount_EsRole (
        users_uuid varchar(255) not null,
        roles_uuid varchar(255) not null,
        primary key (users_uuid, roles_uuid)
    );

-- schema update # 4

    create table EsAuth (
        uuid varchar(255) not null,
        description longtext,
        name varchar(255) not null,
        primary key (uuid)
    );

-- schema update # 5

    create table EsAuth_EsRole (
        auths_uuid varchar(255) not null,
        roles_uuid varchar(255) not null,
        primary key (auths_uuid, roles_uuid)
    );

-- schema update # 6

    create table EsRole (
        uuid varchar(255) not null,
        description longtext,
        name varchar(255) not null,
        primary key (uuid)
    );

-- schema update # 7

    alter table EsAccount_EsRole 
        add index FK_7917kovyd7cq1km9n63u97sf0 (roles_uuid), 
        add constraint FK_7917kovyd7cq1km9n63u97sf0 
        foreign key (roles_uuid) 
        references EsRole (uuid);

-- schema update # 8

    alter table EsAccount_EsRole 
        add index FK_m75gnehjdyujyi59wmr6yt8wt (users_uuid), 
        add constraint FK_m75gnehjdyujyi59wmr6yt8wt 
        foreign key (users_uuid) 
        references EsAccount (uuid);

-- schema update # 9

    alter table EsAuth_EsRole 
        add index FK_i4kgwy1ei5bs5vs8u8e6fd1b5 (roles_uuid), 
        add constraint FK_i4kgwy1ei5bs5vs8u8e6fd1b5 
        foreign key (roles_uuid) 
        references EsRole (uuid);

-- schema update # 10

    alter table EsAuth_EsRole 
        add index FK_p17iy1pib19ayens8cjiay8r2 (auths_uuid), 
        add constraint FK_p17iy1pib19ayens8cjiay8r2 
        foreign key (auths_uuid) 
        references EsAuth (uuid);

# --- !Downs