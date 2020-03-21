<p align="center">
<img src="https://raw.githubusercontent.com/aelkz/microservices-coffeeshop/master/_images/78CDD2957A571A75D111D5D8BFAEC34E.png" title="Microservices Coffee Shop" />
</p>

### 1-INIT
```
export USER=rabreu
export PROJECT_NS=microservices-coffeeshop
export OCP_HOST=mycloud.com

oc login -u $USER https://api.$OCP_HOST.com:6443

oc new-project $PROJECT_NS --description="Expert Extra Recording LAB" --display-name="microservices-coffeeshop"
```

### 2-RED HAT REGISTRY SECRET CONFIGURATION
```
cat > config.json <<EOL
{
        "auths": {
                "registry.redhat.io": {
                        "auth": "YOUR-SECRET-KEY"
                }
        }
}
EOL
```

#### 2.1- Create a secret with your container credentials
```
oc delete secret redhat.io -n openshift
oc create secret generic "redhat.io" --from-file=.dockerconfigjson=config.json --type=kubernetes.io/dockerconfigjson -n $PROJECT_NS

oc secrets link default redhat.io --for=pull -n $PROJECT_NS
oc secrets link builder redhat.io -n $PROJECT_NS
```

### 3-IMPORT OPENJDK-8-RHEL8 CONTAINER IMAGE
```
# option1
oc import-image openjdk/openjdk-8-rhel8 --from=registry.redhat.io/openjdk/openjdk-8-rhel8 --confirm -n $PROJECT_NS
oc import-image openjdk/openjdk-8-rhel8 --from=registry.redhat.io/openjdk/openjdk-8-rhel8 --confirm -n openshift
# option2 (working)
oc import-image redhat-openjdk-18/openjdk18-openshift --from=registry.redhat.io/redhat-openjdk-18/openjdk18-openshift --confirm -n openshift
```

### 4-NEXUS OSS DEPLOYMENT

```
oc new-app --docker-image docker.io/sonatype/nexus3:latest

oc rollout pause dc/nexus3

oc patch dc nexus3 -p '{"spec":{"strategy":{"type":"Recreate"}}}'

oc expose svc nexus3

oc set resources dc nexus3 --limits=memory=4Gi,cpu=2 --requests=memory=2Gi,cpu=500m

oc set volume dc/nexus3 --add --overwrite --name=nexus3-volume-1 --mount-path=/nexus-data/ --type persistentVolumeClaim --claim-name=nexus-pvc --claim-size=10Gi

oc set probe dc/nexus3 --liveness --failure-threshold 3 --initial-delay-seconds 60 -- echo ok

oc set probe dc/nexus3 --readiness --failure-threshold 3 --initial-delay-seconds 60 --get-url=http://:8081/

oc rollout resume dc nexus3

NEXUS_POD=$(oc get pods --selector deploymentconfig=nexus3 -n $PROJECT_NS | { read line1 ; read line2 ; echo "$line2" ; } | awk '{print $1;}')

oc exec $NEXUS_POD cat /nexus-data/admin.password
# It will print the password. Something like:
# 33456b65-7e85-4dfc-a063-78b413cf4a47

echo http://$(oc get route -n ${PROJECT_NS} | grep nexus3 | awk '{print $2;}')/

# log into the nexus and change password to admin123 (also enable: Enable anonymous access)
# then, download the shell script bellow:
curl -o setup_nexus3.sh -s https://raw.githubusercontent.com/aelkz/microservices-coffeeshop/master/_configuration/nexus/setup_nexus3.sh

chmod +x setup_nexus3.sh

./setup_nexus3.sh admin admin123 http://$(oc get route -n ${PROJECT_NS} nexus3 --template='{{ .spec.host }}')

oc expose dc nexus3 --port=5000 --name=nexus-registry -n ${PROJECT_NS}

oc create route edge nexus-registry --service=nexus-registry --port=5000 -n ${PROJECT_NS}
```

<p align="center">
<img src="https://raw.githubusercontent.com/aelkz/microservices-coffeeshop/master/_images/D0A39464F60F43E313DA1E8060A1C272.png" />
</p>

<b>OBS</b>.: You might want to add the jboss.org repository and add to `maven-group` and `maven-public` repositories on nexus:
See: parent project `pom.xml` for details on `<repositories>` tag.

<p align="center">
<img src="https://raw.githubusercontent.com/aelkz/microservices-coffeeshop/master/_images/2B9E458A4AF1189513F4988DB9573639.png" />
</p>

### 5-NEXUS ENVIRONMENT VARIABLES
```
# download maven settings.xml file
curl -o maven-settings-template.xml -s https://raw.githubusercontent.com/aelkz/microservices-coffeeshop/master/_configuration/nexus/maven-settings-template.xml

# change mirror url using your nexus openshift route
export MAVEN_URL=http://$(oc get route -n ${PROJECT_NS} nexus3 --template='{{ .spec.host }}')/repository/maven-group/
export MAVEN_URL_RELEASES=http://$(oc get route -n ${PROJECT_NS} nexus3 --template='{{ .spec.host }}')/repository/maven-releases/
export MAVEN_URL_SNAPSHOTS=http://$(oc get route -n ${PROJECT_NS} nexus3 --template='{{ .spec.host }}')/repository/maven-snapshots/

awk -v path="$MAVEN_URL" '/<url>/{sub(/>.*</,">"path"<")}1' maven-settings-template.xml > maven-settings.xml

rm -fr maven-settings-template.xml
```

### 6-APPLICATION DEPLOYMENT - PARENT PROJECT
```
# deploy parent project on nexus
git clone https://github.com/aelkz/microservices-coffeeshop.git

mvn clean package deploy -DnexusReleaseRepoUrl=$MAVEN_URL_RELEASES -DnexusSnapshotRepoUrl=$MAVEN_URL_SNAPSHOTS -s ./maven-settings.xml -e -X -N
```

### 7-APPLICATION DEPLOYMENT - BARISTA THORNTAIL APPLICATION
```
export APP_NAME=barista
export THORNTAIL_APP=$APP_NAME-service
# oc delete all -lapp=$APP_NAME-service -n $PROJECT_NS
# oc delete is $APP_NAME-service -n $PROJECT_NS
# oc delete bc $APP_NAME-service-s2i -n $PROJECT_NS
# oc delete is $APP_NAME-service -n $PROJECT_NS
```

##### Prefered Option (using a source strategy build):
```
oc new-app openjdk18-openshift:latest~https://github.com/aelkz/microservices-coffeeshop.git --name=$THORNTAIL_APP --context-dir=/$APP_NAME-service --build-env='MAVEN_MIRROR_URL='${MAVEN_URL} -e MAVEN_MIRROR_URL=${MAVEN_URL} -n ${PROJECT_NS}
```

### 8-CONFIGURE APP RESOURCES
```
oc tag $APP_NAME-service:1.0 $APP_NAME-service:latest -n ${PROJECT_NS}
oc set resources dc $THORNTAIL_APP --requests=cpu=500m,memory=500Mi --limits=cpu=750m,memory=1Gi -n ${PROJECT_NS}
oc set env dc/$THORNTAIL_APP JAVA_DEBUG=true -n ${PROJECT_NS}
oc set env dc/$THORNTAIL_APP GC_MAX_METASPACE_SIZE=256 -n ${PROJECT_NS}
```

### 9-CONFIGURE APP PRODUCTION ENVIRONMENT
```
oc delete configmap $APP_NAME-config-file -n ${PROJECT_NS}
oc create -f ../_configuration/openshift/$APP_NAME-config/production.yaml -n ${PROJECT_NS}

oc set volume dc/$APP_NAME-service --remove --name=$APP_NAME-config-volume -n ${PROJECT_NS}
oc set volume dc/$APP_NAME-service --add --overwrite --name=$APP_NAME-config-volume -m /app/config -t configmap --configmap-name=$APP_NAME-config-file -n ${PROJECT_NS}

oc set env dc/$APP_NAME-service JAVA_OPTIONS="-server -Djava.net.preferIPv4Stack=true -Dswarm.project.stage=production -Dthorntail.project.stage.file=file:///app/config/$APP_NAME-config.yml"
```

### 10-EXPOSE APP ROUTE

```
oc expose svc/$APP_NAME-service -n ${PROJECT_NS}
```

### TEST!

<p align="center">
<img src="https://raw.githubusercontent.com/aelkz/microservices-coffeeshop/master/_images/C2889DA640D07E02D4E4C134B56BA0A8.png" />
</p>

### 11-APPLICATION DEPLOYMENT - PRODUCT THORNTAIL APPLICATION
```
APP_NAME=product
THORNTAIL_APP=$APP_NAME-service
# oc delete all -lapp=$APP_NAME-service -n $PROJECT_NS
# oc delete is $APP_NAME-service -n $PROJECT_NS
# oc delete bc $APP_NAME-service-s2i -n $PROJECT_NS
# oc delete is $APP_NAME-service -n $PROJECT_NS

oc new-app openjdk18-openshift:latest~https://github.com/aelkz/microservices-coffeeshop.git --name=$THORNTAIL_APP --context-dir=/$APP_NAME-service --build-env='MAVEN_MIRROR_URL='${MAVEN_URL} -e MAVEN_MIRROR_URL=${MAVEN_URL} -n ${PROJECT_NS}

oc tag $APP_NAME-service:1.0 $APP_NAME-service:latest -n ${PROJECT_NS}
oc set resources dc $THORNTAIL_APP --requests=cpu=500m,memory=500Mi --limits=cpu=750m,memory=1Gi -n ${PROJECT_NS}
oc set env dc/$THORNTAIL_APP GC_MAX_METASPACE_SIZE=256 -n ${PROJECT_NS}

oc expose svc/$APP_NAME-service -n ${PROJECT_NS}
```

### 12-APPLICATION DEPLOYMENT - STORAGE THORNTAIL APPLICATION
```
APP_NAME=storage
THORNTAIL_APP=$APP_NAME-service
# oc delete all -lapp=$APP_NAME-service -n $PROJECT_NS
# oc delete is $APP_NAME-service -n $PROJECT_NS
# oc delete bc $APP_NAME-service-s2i -n $PROJECT_NS
# oc delete is $APP_NAME-service -n $PROJECT_NS

oc new-app openjdk18-openshift:latest~https://github.com/aelkz/microservices-coffeeshop.git --name=$THORNTAIL_APP --context-dir=/$APP_NAME-service --build-env='MAVEN_MIRROR_URL='${MAVEN_URL} -e MAVEN_MIRROR_URL=${MAVEN_URL} -n ${PROJECT_NS}

oc tag $APP_NAME-service:1.0 $APP_NAME-service:latest -n ${PROJECT_NS}
oc set resources dc $THORNTAIL_APP --requests=cpu=500m,memory=500Mi --limits=cpu=750m,memory=1Gi -n ${PROJECT_NS}
oc set env dc/$THORNTAIL_APP GC_MAX_METASPACE_SIZE=256 -n ${PROJECT_NS}

oc expose svc/$APP_NAME-service -n ${PROJECT_NS}
```

### 13-APPLICATION DEPLOYMENT - MAINTENANCE THORNTAIL APPLICATION
```
APP_NAME=maintenance
THORNTAIL_APP=$APP_NAME-service
# oc delete all -lapp=$APP_NAME-service -n $PROJECT_NS
# oc delete is $APP_NAME-service -n $PROJECT_NS
# oc delete bc $APP_NAME-service-s2i -n $PROJECT_NS
# oc delete is $APP_NAME-service -n $PROJECT_NS

oc new-app openjdk18-openshift:latest~https://github.com/aelkz/microservices-coffeeshop.git --name=$THORNTAIL_APP --context-dir=/$APP_NAME-service --build-env='MAVEN_MIRROR_URL='${MAVEN_URL} -e MAVEN_MIRROR_URL=${MAVEN_URL} -n ${PROJECT_NS}

oc tag $APP_NAME-service:1.0 $APP_NAME-service:latest -n ${PROJECT_NS}
oc set resources dc $THORNTAIL_APP --requests=cpu=500m,memory=500Mi --limits=cpu=750m,memory=1Gi -n ${PROJECT_NS}
oc set env dc/$THORNTAIL_APP GC_MAX_METASPACE_SIZE=256 -n ${PROJECT_NS}

oc expose svc/$APP_NAME-service -n ${PROJECT_NS}
```

### 14-APPLICATION DEPLOYMENT - ORDER THORNTAIL APPLICATION
```
APP_NAME=order
THORNTAIL_APP=$APP_NAME-service
# oc delete all -lapp=$APP_NAME-service -n $PROJECT_NS
# oc delete is $APP_NAME-service -n $PROJECT_NS
# oc delete bc $APP_NAME-service-s2i -n $PROJECT_NS
# oc delete is $APP_NAME-service -n $PROJECT_NS

oc new-app openjdk18-openshift:latest~https://github.com/aelkz/microservices-coffeeshop.git --name=$THORNTAIL_APP --context-dir=/$APP_NAME-service --build-env='MAVEN_MIRROR_URL='${MAVEN_URL} -e MAVEN_MIRROR_URL=${MAVEN_URL} -n ${PROJECT_NS}

oc tag $APP_NAME-service:1.0 $APP_NAME-service:latest -n ${PROJECT_NS}
oc set resources dc $THORNTAIL_APP --requests=cpu=500m,memory=500Mi --limits=cpu=750m,memory=1Gi -n ${PROJECT_NS}
oc set env dc/$THORNTAIL_APP GC_MAX_METASPACE_SIZE=256 -n ${PROJECT_NS}

APP_NAME=order
BARISTA_ROUTE=http://barista-service.${PROJECT_NS}.svc.cluster.local:8080/api/v1
MAINTENANCE_ROUTE=http://maintenance-service.${PROJECT_NS}.svc.cluster.local:8080/api/v1
PRODUCT_ROUTE=http://product-service.${PROJECT_NS}.svc.cluster.local:8080/api/v1
STORAGE_ROUTE=http://storage-service.${PROJECT_NS}.svc.cluster.local:8080/api/v1

echo "
apiVersion: v1
kind: ConfigMap
metadata:
 name: order-config-file
data:
  order-config.yml: |-
    project:
      stage: production
    thorntail:
      logging: INFO
      ajp:
        enable: true
      http:
        port: 8080
      datasources:
        data-sources:
          OrderDS:
            driver-name: h2
            connection-url: jdbc:h2:mem:order;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
            user-name: sa
            password: sa
    greeting:
      message: Hello %s from order-configmap.yml!
    coffeeshop:
      routes:
        barista-service: \""$BARISTA_ROUTE"\"
        maintenance-service: \""$MAINTENANCE_ROUTE\""
        product-service: \""$PRODUCT_ROUTE\""
        storage-service: \""$STORAGE_ROUTE\""
" > production.yaml

oc delete configmap $APP_NAME-config-file -n ${PROJECT_NS}
oc create -f production.yaml -n ${PROJECT_NS}

# oc set volume dc/$APP_NAME-service --remove --name=$APP_NAME-config-volume -n ${PROJECT_NS}
oc set volume dc/$APP_NAME-service --add --overwrite --name=$APP_NAME-config-volume -m /app/config -t configmap --configmap-name=$APP_NAME-config-file -n ${PROJECT_NS}

oc set env dc/$APP_NAME-service JAVA_OPTIONS="-server -Djava.net.preferIPv4Stack=true -Dswarm.project.stage=production -Dthorntail.project.stage.file=file:///app/config/$APP_NAME-config.yml"

oc expose svc/$APP_NAME-service -n ${PROJECT_NS}
```

### 15-PROMETHEUS DEPLOYMENT

All microprofile applications enable by default the following endpoints:
http://localhost:8080/metrics (all metrics)
http://localhost:8080/metrics/application (exclusively for application-level metrics)

```
echo "
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: thorntail-prometheus-monitor
  labels:
    k8s-app: prometheus
  namespace: "$PROJECT_NS"
spec:
  namespaceSelector:
    matchNames:
      - "$PROJECT_NS"
  selector:
    matchLabels:
      monitor: thorntail-app
  endpoints:
    - port: http
      interval: 15s
      path: '/metrics'
"      
```

```
echo "
apiVersion: monitoring.coreos.com/v1
kind: Prometheus
metadata:
  name: server
  labels:
    prometheus: k8s
  namespace: "$PROJECT_NS"
spec:
  replicas: 2
  serviceAccountName: prometheus-k8s
  securityContext: {}
  serviceMonitorSelector:
    matchExpressions:
      - key: k8s-app
        operator: Exists
  ruleSelector:
    matchLabels:
      role: prometheus-rulefiles
      prometheus: k8s
  alerting:
    alertmanagers:
      - namespace: "$PROJECT_NS"
        name: alertmanager-main
        port: web
"        
```

Expose prometheus route:
`oc expose svc prometheus-operated -n $PROJECT_NS`

### 16-GRAFANA DEPLOYMENT

Follow these instructions:
https://www.redhat.com/en/blog/custom-grafana-dashboards-red-hat-openshift-container-platform-4
https://github.com/integr8ly/application-monitoring-operator/blob/master/scripts/install.sh
https://medium.com/@zhimin.wen/grafana-dashboard-in-ocp4-2-44468e5390d0
https://operatorhub.io/operator/grafana-operator

Grafana operator template:
```
echo "
apiVersion: integreatly.org/v1alpha1
kind: Grafana
metadata:
  name: grafana
  namespace: "$PROJECT_NS"
spec:
  ingress:
    enabled: true
  config:
    auth.anonymous:
      enabled: true
    log:
      level: warn
      mode: console
    security:
      admin_password: "123456"
      admin_user: "grafana"
    auth:
      disable_login_form: false
      disable_signout_menu: true
  dashboardLabelSelector:
    - matchExpressions:
        - key: app
          operator: In
          values:
            - grafana
"
```

You can also deploy the objects manually:
```
git clone https://github.com/integr8ly/grafana-operator.git
cd grafana-operator
oc create -f deploy/crds -n ${PROJECT_NS}
oc create -f deploy/roles -n ${PROJECT_NS}
oc create -f deploy/cluster_roles -n ${PROJECT_NS}
oc create -f deploy/operator.yaml -n ${PROJECT_NS}
```

OBS. Grafana can be deployed with a oauth-proxy sidecard.
In order to access grafana, you'll need access using some service-account and/or cluster admin.

Acquire grafana's route
```
echo http://$(oc get route grafana -n ${PROJECT_NS} --template='{{ .spec.host }}')
```

Acquire prometheus route
```
echo http://prometheus-operated.$PROJECT_NS.svc.cluster.local:9090
```

Grafana should be configurated to use the Prometheus datasouce:
```
URL= http://prometheus-operated.<YOUR-NAMESPACE>.svc.cluster.local:9090
Scrape Interval=5s
Query Timeout=30s
```

Apply and save. Check if the connection was successful:

<p align="center">
<img src="https://raw.githubusercontent.com/aelkz/microservices-coffeeshop/master/_images/9284EAE7A078631050A3F2EB9CF4B351.png" />
</p>

### APPLY PROMETHEUS LABELS
#### This will add the prometheus label, to enable scrapping on all services as defined in the step 15.


<p align="center">
We will update all services and routes, changing from this:<br>
<img src="https://raw.githubusercontent.com/aelkz/microservices-coffeeshop/master/_images/A8FF0B41453F76DC82EFE65348C5B117.png" />
<br>To this:<br>
<img src="https://raw.githubusercontent.com/aelkz/microservices-coffeeshop/master/_images/77C55CBC16AA2D83CB3D17E50E76D63B.png" />
</p>

```
oc label svc barista-service monitor=thorntail-app -n $PROJECT_NS
oc patch svc barista-service -p '{"spec":{"ports":[{"name":"http","port":8080,"protocol":"TCP","targetPort":8080}]}}' -n $PROJECT_NS
oc patch route barista-service -p '{"spec":{"port":{"targetPort":"http"}}}' -n $PROJECT_NS

oc label svc maintenance-service monitor=thorntail-app -n $PROJECT_NS
oc patch svc maintenance-service -p '{"spec":{"ports":[{"name":"http","port":8080,"protocol":"TCP","targetPort":8080}]}}' -n $PROJECT_NS
oc patch route maintenance-service -p '{"spec":{"port":{"targetPort":"http"}}}' -n $PROJECT_NS

oc label svc product-service monitor=thorntail-app -n $PROJECT_NS
oc patch svc product-service -p '{"spec":{"ports":[{"name":"http","port":8080,"protocol":"TCP","targetPort":8080}]}}' -n $PROJECT_NS
oc patch route product-service -p '{"spec":{"port":{"targetPort":"http"}}}' -n $PROJECT_NS

oc label svc storage-service monitor=thorntail-app -n $PROJECT_NS
oc patch svc storage-service -p '{"spec":{"ports":[{"name":"http","port":8080,"protocol":"TCP","targetPort":8080}]}}' -n $PROJECT_NS
oc patch route storage-service -p '{"spec":{"port":{"targetPort":"http"}}}' -n $PROJECT_NS

oc label svc order-service monitor=thorntail-app -n $PROJECT_NS
oc patch svc order-service -p '{"spec":{"ports":[{"name":"http","port":8080,"protocol":"TCP","targetPort":8080}]}}' -n $PROJECT_NS
oc patch route order-service -p '{"spec":{"port":{"targetPort":"http"}}}' -n $PROJECT_NS
```

After this, you can check creation of all service targets on prometheus:

<p align="center">
<img src="https://raw.githubusercontent.com/aelkz/microservices-coffeeshop/master/_images/6B42ECA6F89BE517DACA7B44D44C2F40.png" />
</p>

### TEST USING REST HTTP CALLS
Open your Rest client application and try to create some records.
You'll see the final result being updated uppon grafana dashboard.

<p align="center">
<img src="https://raw.githubusercontent.com/aelkz/microservices-coffeeshop/master/_images/48D5C0DCCC89ECC3B7720D4472BA10B4.png" />
</p>
