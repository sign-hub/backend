#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    alter table Media 
        add column thumbPath varchar(255) default null;

# --- !Downs