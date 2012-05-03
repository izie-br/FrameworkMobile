from testproject.testapp.models import Document, Author
from django.http import HttpResponse
import json

def get_documents_json(request,id=None):
    documents = Document.objects.all()
    #jserializer = serializers.get_serializer('json')()
    jsonobj = json.dumps(
        [{
            'id':doc.pk,
            'text':doc.text,
            'titulo':doc.title
        }  for doc in documents],
        ensure_ascii=False
    )
    return HttpResponse(jsonobj)

def get_authors_json(request,id=None):
    authors = Author.objects.all()
    jsonobj = json.dumps(
        [ a.map for a in authors ],
        ensure_ascii=False
    )
    return HttpResponse(jsonobj)

