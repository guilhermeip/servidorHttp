function sortName() {
  var table, rows, switching, i, x, y, shouldSwitch;
  table = document.getElementById("myTable");
  switching = true;
  /* Make a loop that will continue until
  no switching has been done: */
  while (switching) {
    // Start by saying: no switching is done:
    switching = false;
    rows = table.rows;
    /* Loop through all table rows (except the
    first, which contains table headers): */
    for (i = 2; i < (rows.length - 1); i++) {
      // Start by saying there should be no switching:
      shouldSwitch = false;
      /* Get the two elements you want to compare,
      one from current row and one from the next: */
      x = rows[i].getElementsByTagName("TD")[0];
      y = rows[i + 1].getElementsByTagName("TD")[0];
      // Check if the two rows should switch place:
      if (x.innerHTML.toLowerCase() > y.innerHTML.toLowerCase()) {
        // If so, mark as a switch and break the loop:
        shouldSwitch = true;
        break;
      }
    }
    if (shouldSwitch) {
      /* If a switch has been marked, make the switch
      and mark that a switch has been done: */
      rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
      switching = true;
    }
  }
}

function sortTam() {
  var table, rows, switching, i, x, y, shouldSwitch;
  table = document.getElementById("myTable");
  switching = true;
  /* Make a loop that will continue until
  no switching has been done: */
  while (switching) {
    // Start by saying: no switching is done:
    switching = false;
    rows = table.rows;
    /* Loop through all table rows (except the
    first, which contains table headers): */
    for (i = 2; i < (rows.length - 1); i++) {
      // Start by saying there should be no switching:
      shouldSwitch = false;
      /* Get the two elements you want to compare,
      one from current row and one from the next: */
	x_row = rows[i].getElementsByTagName("TD")[1].innerHTML;   
	x = x_row.split(" ");
	x_num = Number(x[0]);
	x_bytes = x[1];

      y_row = rows[i + 1].getElementsByTagName("TD")[1].innerHTML;
	y = y_row.split(" ");
	y_num = Number(y[0]);
	y_bytes = y[1];
	
	
	if(x_bytes){
		if(x_bytes.startsWith("G")){
			x_num = x_num*1000*1000*1000;
		}else if(x_bytes.startsWith("M")){
			x_num = x_num*1000*1000;
		}else if(x_bytes.startsWith("k")){
			x_num *= 1000;
		}
	}
	if(y_bytes){
		if(y_bytes.startsWith("G")){
			y_num = y_num*1000*1000*1000;
		}else if(y_bytes.startsWith("M")){
			y_num = y_num*1000*1000;
		}else if(y_bytes.startsWith("k")){
			y_num *= 1000;
		}
	}
	
      // Check if the two rows should switch place:
      if (x_num > y_num) {
        // If so, mark as a switch and break the loop:
        shouldSwitch = true;
        break;
      }
    }
    if (shouldSwitch) {
      /* If a switch has been marked, make the switch
      and mark that a switch has been done: */
      rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
      switching = true;
    }
  }
}