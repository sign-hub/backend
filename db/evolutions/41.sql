#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    alter table FeatureValue 
        add column personalJudgment boolean;

-- schema update # 2

    alter table SignLanguage 
        add column ackLEX longtext;

-- schema update # 3

    alter table SignLanguage 
        add column ackMORPH longtext;

-- schema update # 4

    alter table SignLanguage 
        add column ackPHON longtext;

-- schema update # 5

    alter table SignLanguage 
        add column ackPRAG longtext;

-- schema update # 6

    alter table SignLanguage 
        add column ackSYN longtext;

-- schema update # 7

    alter table SignLanguage 
        add column cpLEX varchar(255);

-- schema update # 8

    alter table SignLanguage 
        add column cpMORPH varchar(255);

-- schema update # 9

    alter table SignLanguage 
        add column cpPHON varchar(255);

-- schema update # 10

    alter table SignLanguage 
        add column cpPRAG varchar(255);

-- schema update # 11

    alter table SignLanguage 
        add column cpSYN varchar(255);