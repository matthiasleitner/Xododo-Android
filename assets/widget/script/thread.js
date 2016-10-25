var i = 0;
var db = false;
function run()
{
    try{
        if(!db)
        {
            db = openDatabaseSync('contactdb', '', 'local database demo', 204800);
        }
        postMessage((i++));
    }
    catch(e)
    {
        postMessage(e.message);
        
    }
    
}

setInterval(run, 1000);