#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    alter table GrammarPart 
        add column completeOrderNow float;