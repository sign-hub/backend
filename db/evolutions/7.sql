#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    alter table Question 
        add column creationDate datetime;

-- schema update # 2

    alter table Question 
        add column orderNum integer;

-- schema update # 3

    alter table Slide 
        add column creationDate datetime;

-- schema update # 4

    alter table Slide 
        add column orderNum integer;

# --- !Downs
