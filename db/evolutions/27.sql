#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    alter table Feature 
        add column bluePrintSection varchar(255);

-- schema update # 2

    alter table Feature 
        add column groupName varchar(255);

-- schema update # 3

    alter table Feature 
        add column personalJudgment boolean;

-- schema update # 4

    alter table Feature 
        add column sectionName varchar(255);

-- schema update # 5

    alter table Feature 
        add column slideName varchar(255);

-- schema update # 6

    alter table Feature 
        add column testName varchar(255);

