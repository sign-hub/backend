#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    alter table Media 
        add column publicCode varchar(255);