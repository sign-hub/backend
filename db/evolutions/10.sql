#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    create table Topic (
        uuid varchar(255) not null,
        definition longtext,
        word varchar(255),
        primary key (uuid)
    );
        
# --- !Downs
