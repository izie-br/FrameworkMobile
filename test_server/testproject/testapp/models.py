from django.db import models

class Document(models.Model):
    title = models.CharField(max_length=79)
    text = models.TextField()
    author = models.ForeignKey('Author')

    def get_map(self):
        return {'id':self.pk,'title':self.title,'text':self.text,'author_id':self.author.pk}

    def set_map(self,values):
        self.pk = values['id'] if 'id' in values else None
        self.title = values['title']
        self.text = values['text']
        self.author = Author.objects().get(values['author_id'])

    map = property(get_map,set_map)

    def __unicode__(self):
        return self.title

class Author(models.Model):
    name = models.CharField(max_length=79,unique=True)

    def get_map(self):
        return {'id':self.pk,'name':self.name}

    def set_map(self,values):
        self.pk = values['id'] if 'id' in values else None
        self.name = values['name']

    map = property(get_map,set_map)

    def __unicode__(self):
        return self.name
