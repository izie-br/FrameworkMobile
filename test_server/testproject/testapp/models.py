from django.db import models

class Document(models.Model):
    title = models.CharField(max_length=79)
    text = models.TextField()
    author = models.ForeignKey('Author')

    def get_map(self):
        return {'id':self.pk,'title':self.title,'text':self.text,'author_id':self.author.pk}
    map = property(get_map)

    def __unicode__(self):
        return self.title

class Author(models.Model):
    name = models.CharField(max_length=79,unique=True)

    def get_map(self):
        return {'id':self.pk,'name':self.name}

    map = property(get_map)

    def __unicode__(self):
        return self.name
