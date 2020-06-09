#
# versione base database CINI
#

# --- !Ups

    alter table SignLanguage 
        add column area varchar(255);

-- schema update # 2

    alter table SignLanguage 
        add column status varchar(255);
