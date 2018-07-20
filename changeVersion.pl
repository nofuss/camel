#!/usr/bin/perl -i.bak -w
use strict;
use File::Find;

# helpful: find . -name pom.xml -print0 | xargs -0 grep 2.23.0-SNAPSHOT '{}' +
# helpful: find . -name pom.xml.bak -print0 | xargs -0 rm -f '{}' +

my $versionFrom = '2.23.0-SNAPSHOT';
my $versionTo = '2.23.0-MP-1.0.0';

my @files;
find(\&wanted, '.');

{
    local @ARGV = @files;
    while (<>) {
        s/$versionFrom/$versionTo/;  # modify the file
        print;                       # print the modifications
    }
}

sub wanted { 
    return unless $_ eq 'pom.xml';
    push @files, $File::Find::name;
}
