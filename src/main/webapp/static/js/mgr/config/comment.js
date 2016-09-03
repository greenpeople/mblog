$(document).ready(function() {
	$(".integer").on('keydown', function(e){-1!==$.inArray(e.keyCode,[46,8,9,27,13,110])||/65|67|86|88/.test(e.keyCode)&&(!0===e.ctrlKey||!0===e.metaKey)||35<=e.keyCode&&40>=e.keyCode||(e.shiftKey||48>e.keyCode||57<e.keyCode)&&(96>e.keyCode||105<e.keyCode)&&e.preventDefault()});
	$("#update").click(function() {
		var data = $("#commentConfigForm").serializeObject();
		var config = {};
		config.limit = {"limit":data.count,"time":data.time};
		config.asc = data.sort == 'asc';
		$.ajax({
			type : "post",
			url : basePath + "/mgr/config/comment/update",
			data : JSON.stringify(config),
			dataType : "json",
			contentType : 'application/json',
			success : function(data) {
				if (data.success) {
					success(data.message);
					setTimeout(function() {
						window.location.reload();
					}, 500);
				} else {
					error(data.message);
				}
			},
			complete : function() {
				$("#update").prop("disabled", false);
			}
		});
	});
});