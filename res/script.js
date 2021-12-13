var bar = document.getElementById('js-progressbar');
UIkit.upload('.js-upload', {
    url: '/api/general/files/upload',
    multiple: true,
    name: "file",
    beforeSend: function () {
        console.log('beforeSend', arguments);
    },
    beforeAll: function () {
        console.log('beforeAll', arguments);
    },
    load: function () {
        console.log('load', arguments);
    },
    error: function () {
        console.log('error', arguments);
    },
    complete: function (e) {
        console.log('complete', e);
        console.log('complete', arguments);
    },
    loadStart: function (e) {
        console.log('loadStart', arguments);
        bar.removeAttribute('hidden');
        bar.max = e.total;
        bar.value = e.loaded;
    },
    progress: function (e) {
        console.log('progress', arguments);
        bar.max = e.total;
        bar.value = e.loaded;
    },
    loadEnd: function (e) {
        console.log('loadEnd', arguments);
        bar.max = e.total;
        bar.value = e.loaded;
    },
    completeAll: function () {
        console.log('completeAll', arguments);
        setTimeout(function () {
            bar.setAttribute('hidden', 'hidden');
        }, 1000);
        reload();
    }
});

$.ajaxSetup({
    contentType: "application/json; charset=utf-8"
});

function humanFileSize(bytes, si = false, dp = 1) {
    const thresh = si ? 1000 : 1024;
    if (Math.abs(bytes) < thresh) {
        return bytes + '\xa0';
    }
    const units = ['kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
    let u = -1;
    const r = 10 ** dp;
    do {
        bytes /= thresh;
        ++u;
    } while (Math.round(Math.abs(bytes) * r) / r >= thresh && u < units.length - 1);
    return bytes.toFixed(dp) + '\xa0' + units[u];
}

function sendGet(sendUrl) {
    $.ajax({
        url: sendUrl,
        type: "GET",
        error: function (data, status) {
            reload();
        },
        success: function (data, status) {
            reload();
        }
    });
}

function reload() {
    $("#downloadList").empty();
    $("#downloadList").hide();
    $("#Spinner").show();

    $.ajax({
        url: "/api/general/files/list",
        type: "GET",
        error: function (data, status) {
            onerror(data);
        },
        success: function (data, status) {
            $("#Spinner").hide();
            $("#downloadList").fadeIn(500);
            for (var key in data) {
                var div = document.createElement('div');
                div.classList.add("CenteredItems");
                div.style.display = "inline-flex";
                div.style.flexDirection = "row";

                var fileIcon = document.createElement('img');
                fileIcon.src = data[key].isDirectory ? "folder.svg" : "file.svg";
                fileIcon.style.width = "28px";
                fileIcon.style.paddingLeft = "5px";
                fileIcon.style.paddingRight = "5px";
                div.append(fileIcon);

                var fileLink = document.createElement('a');
                fileLink.appendChild(document.createTextNode(data[key].name));
                fileLink.href = "/api/general/files/download/" + data[key].name;
                fileLink.classList.add("HeaderHeight");
                fileLink.classList.add("FileEntryName");
                fileLink.classList.add("BCLinkBlue");
                div.append(fileLink);

                if (!data[key].isDirectory) {
                    var fileSize = document.createElement('span');
                    fileSize.classList.add("HeaderHeight");
                    fileSize.classList.add("FileEntrySize");
                    fileSize.classList.add("BCCLightGray");
                    fileSize.appendChild(document.createTextNode("" + humanFileSize(data[key].length)));
                    div.append(fileSize);
                }

                var fileDelete = document.createElement('a');
                fileDelete.innerHTML = "&nbsp";
                fileDelete.classList.add("HeaderHeight");
                fileDelete.classList.add("FileEntryDelete");
                var deleteIcon = document.createElement('img');
                deleteIcon.src = "delete.svg";
                deleteIcon.style.width = "15px";
                deleteIcon.style.height = "15px";
                fileDelete.style.textDecoration = "none";
                fileDelete.appendChild(deleteIcon);
                fileDelete.data = "/api/general/files/delete/" + data[key].name;
                fileDelete.onclick = function () {
                    sendGet(this.data);
                };
                div.append(fileDelete);

                $("#downloadList").append(div);
            }
        }
    });
}

reload();
