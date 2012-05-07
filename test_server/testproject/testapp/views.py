from testproject.testapp.models import Document, Author
from django.http import HttpResponse
import json

def parsejson(classname,objs,*keys_to_list):
    objs = json.loads(objs)
    def _to_object(values):
        klass = globals()[classname]
        obj = klass()
        obj.map = values
        return obj
    for key in keys_to_list:
        objs = objs[key]
    return [ _to_object(i) for i in objs ]

def objects_to_json(objs):
    return json.dumps(
        [ a.map for a in objs ],
        ensure_ascii=False
    )

#######
#VIEWS#
#######

def get_json_objects(request,classname,id=None):
    klass = globals()[classname]
    objs = klass.objects.all()
    jsonobj = objects_to_json(objs)
    return HttpResponse(jsonobj)

def save_json_objects(request,classname):
    json_str = request.POST['json']
    #print objs
    objs = parsejson(classname,json_str,'list')
    for obj in objs:
        obj.pk = None   #garantindo que o id seja null para salvar
        obj.save()
    jsonobjs = objects_to_json(objs)
    return HttpResponse(jsonobjs)

def clear_objects(request,classname):
    klass = globals()[classname]
    klass.objects.all().delete()
    return HttpResponse("OK")

