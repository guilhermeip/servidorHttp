"use strict"


const options = {weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };

function getTam(bytes){
    const GB = bytes/Math.pow(1000, 3);
    const MB = bytes/Math.pow(1000, 2);
    const kB = bytes/1000;
    if(GB > 1){
        return GB + " G";
    }else if(MB > 1){
        return MB + " M";
    }else if(kB > 1){
        return kB + " k";
    }else{
        return bytes + " ";
    }
}


function preencheTable(tbody, vetorJson){
    vetorJson.forEach(element => {
        const tr = document.createElement("tr");
        tr.insertCell(-1).innerHTML = "<a href=" + element.name + ">"+ element.name + "</a>";
        if(element.length != ""){
            tr.insertCell(-1).innerText = getTam(element.length) + "Byte(s)";
        }else{
            tr.insertCell(-1).innerText = element.length;
        }
        tr.insertCell(-1).innerText = new Date(Number(element.lastModified)).toLocaleDateString("pt-BR", options);
        const tdExecute = tr.insertCell(-1);
        if(element.canExecute == "no"){
            tdExecute.innerHTML = '<i class="material-icons">close</i>';
        }else{
            tdExecute.innerHTML = '<i class="material-icons">check</i>';
        }
        if(element.length != ""){
            const a = document.createElement("a");
            a.href = element.name+'?download=true';
            const i = document.createElement("i");
            i.className = "material-icons";
            i.innerText = "cloud_download";
            a.appendChild(i);
            tr.insertCell(-1).appendChild(a);
            }
            else{
            tr.insertCell(-1).innerHTML = '';
        }
        tbody.appendChild(tr);
    });
}

function enviarRequisicaoHttp(){
    const xhttp = new XMLHttpRequest();

    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            const tbody = document.getElementById("tabelaBody");
            const h1 = document.getElementById("titulo");
            const h2 = document.getElementById("items");
            const jsonRes = JSON.parse(this.responseText);
            
            h1.innerText = jsonRes.nameDir;
            h2.innerText = jsonRes.lengthDir;
            
            if(location.pathname != "/"){
                const tr = document.createElement("tr");
                tr.insertCell(-1).innerHTML = "<a href=..>Diretório anterior</a>";
                tr.insertCell(-1).innerText = "";
                tr.insertCell(-1).innerText = "";
                tr.insertCell(-1).innerText = "";
                tr.insertCell(-1).innerText = "";
                tbody.appendChild(tr);
            }
            preencheTable(tbody, jsonRes.listDir);
        }
    };
    xhttp.open("GET", "http://localhost:9090"+location.pathname);
    xhttp.setRequestHeader("Authorization", "Basic " + btoa("admin:admin"));
    xhttp.setRequestHeader("X-Requested-With", "XMLHttpRequest");
    xhttp.send(); 
}
