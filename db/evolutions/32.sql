#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    alter table Feature 
        add column chapterName varchar(255);