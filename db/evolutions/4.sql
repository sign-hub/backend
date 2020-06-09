#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    alter table Media 
        add column repositoryId varchar(255) default null;

# --- !Downs