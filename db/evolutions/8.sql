#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    alter table Media 
        add column testType varchar(255);

-- schema update # 2

    alter table Test 
        add column testType varchar(255);
        
    update Test set testType = 'TESTINGTOOL';
    update Media set testType = 'TESTINGTOOL';

# --- !Downs
