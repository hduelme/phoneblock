import 'package:call_log/call_log.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:phoneblock_mobile/state.dart';

void main() {
  AppState state = AppState(calls: [
    Call(phone: "0123456789", type: Type.iNCOMING, started: 1, duration: 2),
    Call(phone: "0123456789", type: Type.mISSED),
    Call(phone: "0123456789", type: Type.oUTGOING, started: 1, duration: 2),
    Call(phone: "0123456789", type: Type.bLOCKED, rating: Rating.gAMBLE),
  ]);

  runApp(PhoneBlockApp(state));
}

class PhoneBlockApp extends StatelessWidget {
  final AppState state;

  const PhoneBlockApp(this.state, {Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'PhoneBlock mobile',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(state),
    );
  }
}

class MyHomePage extends StatefulWidget {
  final AppState state;

  const MyHomePage(this.state, {Key? key}) : super(key: key);

  @override
  State<MyHomePage> createState() => _MyHomePageState(state);
}

class _MyHomePageState extends State<MyHomePage> {
  AppState state;

  _MyHomePageState(this.state);

  void updateCallList() async {
    Iterable<CallLogEntry> callLog = await CallLog.get();

    setState(() {
      state.calls.clear();
      Set<String> allNumbers = {};
      for (var call in callLog) {
        var callType = type(call.callType);
        if (callType == Type.oUTGOING) {
          // Numbers to which the caller initiated a call should never be blocked.
          continue;
        }
        if (call.name != null) {
          // In the local address book, never attempt to block.
          continue;
        }
        var number = call.number;
        if (number == null) {
          // Strange call without number cannot be blocked anyways.
          continue;
        }
        var newNumber = allNumbers.add(number);
        if (!newNumber) {
          continue;
        }

        state.calls.add(Call(
            phone: phone(call),
            type: callType,
            started: call.timestamp ?? 0,
            duration: call.duration ?? 0,
            label: call.name
        ));
      }
    });
  }

  String phone(CallLogEntry call) {
    return call.number ?? "Unknown";
  }

  Type type(CallType? callType) {
    if (callType == null) {
      return Type.mISSED;
    }
    switch (callType) {
      case CallType.incoming: return Type.iNCOMING;
      case CallType.wifiIncoming: return Type.iNCOMING;

      case CallType.outgoing: return Type.oUTGOING;
      case CallType.wifiOutgoing: return Type.oUTGOING;

      case CallType.rejected: return Type.bLOCKED;
      case CallType.blocked: return Type.bLOCKED;

      case CallType.missed: return Type.mISSED;
      case CallType.voiceMail: return Type.mISSED;
      case CallType.answeredExternally: return Type.mISSED;
      case CallType.unknown: return Type.mISSED;
    }
}

  @override
  Widget build(BuildContext context) {
    // This method is rerun every time setState is called, for instance as done
    // by the _incrementCounter method above.
    //
    // The Flutter framework has been optimized to make rerunning build methods
    // fast, so that you can just rebuild anything that needs updating rather
    // than having to individually change instances of widgets.
    return Scaffold(
      appBar: AppBar(
        title: const Text("PhoneBlock mobile"),
        actions: [
          PopupMenuButton(
            icon: Icon(Icons.menu),
              itemBuilder: (context){
                return [
                  PopupMenuItem<int>(
                    value: 0,
                    child: Text("My Account"),
                  ),

                  PopupMenuItem<int>(
                    value: 1,
                    child: Text("Settings"),
                  ),

                  PopupMenuItem<int>(
                    value: 2,
                    child: Text("Logout"),
                  ),
                ];
              },
              onSelected:(value){
                if(value == 0){
                  print("My account menu is selected.");
                }else if(value == 1){
                  print("Settings menu is selected.");
                }else if(value == 2){
                  print("Logout menu is selected.");
                }
              }
          ),
      ],
      ),
      body: ListView(
        children: widget.state.calls.map((call) =>
          Dismissible(key: Key(call.phone), child:
            ListTile(
              leading: icon(call.type),
              title : Row(mainAxisSize: MainAxisSize.min, children: [
                hint(call.rating),
                Text(call.label ?? call.phone)
              ]),
              subtitle: duration(call),
              trailing: action(call),
            ),
            onDismissed: (direction) => {debugPrint("Dismissed: " + call.phone) },
          )
        ).toList(),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: updateCallList,
        tooltip: 'Increment',
        child: const Icon(Icons.update),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }

  Widget duration(Call call) {
    if (call.started == 0) {
      return const Text("");
    }
    var date = DateTime.fromMillisecondsSinceEpoch(call.started);

    DateFormat format = createFormat(date);
    return Text(format.format(date));
  }

  DateFormat createFormat(DateTime date) {
    var now = DateTime.now();
    var today = DateTime(now.year, now.month, now.day);
    var yesterday = today.subtract(const Duration(days: 1));
    var thisYear = DateTime(today.year);
    
    DateFormat format;
    if (date.isBefore(thisYear)) {
      format = DateFormat('hh:mm dd.MM.yyyy');
    } else if (date.isBefore(yesterday)) {
      format = DateFormat('hh:mm dd.MM.');
    } else if (date.isBefore(today)) {
      format = DateFormat('hh:mm gestern');
    } else {
      format = DateFormat('hh:mm heute');
    }
    return format;
  }

  Widget action(Call call) {
    switch (call.type) {
      case Type.iNCOMING:
        return IconButton(icon: const Icon(Icons.block, color: Colors.redAccent,),
            onPressed: () => Navigator.push(context,
              MaterialPageRoute(builder: (context) => RateScreen(call)),
            )
        );
      case Type.mISSED:
        return IconButton(
            icon: const Icon(Icons.manage_search, color: Colors.blueAccent,),
            onPressed: () => print('Report as spam: ' + call.phone));
      case Type.bLOCKED:
        return IconButton(
            icon: const Icon(Icons.playlist_add, color: Colors.redAccent,),
            onPressed: () => print('Record call: ' + call.phone));
      case Type.oUTGOING:
        return const SizedBox.shrink();
    }
  }

  Widget hint(Rating rating) {
    if (rating == Rating.uNKNOWN || rating == Rating.aLEGITIMATE) {
      return const SizedBox.shrink();
    }
    return Padding(padding: const EdgeInsets.only(right: 5),
        child: Container(
          decoration: BoxDecoration(
              color: bgColor(rating),
              borderRadius: const BorderRadius.all(Radius.circular(10))
          ),
          child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
              child: label(rating)),
        ));
    }

  Icon icon(Type type) {
    switch (type) {
      case Type.bLOCKED: return const Icon(Icons.phone_disabled, color: Colors.redAccent);
      case Type.oUTGOING: return const Icon(Icons.phone_forwarded, color: Colors.green);
      case Type.mISSED: return const Icon(Icons.phone_missed, color: Colors.blueAccent);
      case Type.iNCOMING: return const Icon(Icons.phone_callback, color: Colors.green);
    }
  }
}

Widget label(Rating rating) {
  switch (rating) {
    case Rating.aLEGITIMATE: return const Text("Legitim", style: TextStyle(color: Colors.white));
    case Rating.aDVERTISING: return const Text("Werbung", style: TextStyle(color: Color.fromRGBO(0,0,0,.7)));
    case Rating.uNKNOWN: return const Text("Anderer Grund", style: TextStyle(color: Colors.white));
    case Rating.pING: return const Text("Ping-Anruf", style: TextStyle(color: Colors.white));
    case Rating.gAMBLE: return const Text("Gewinnspiel", style: TextStyle(color: Colors.white));
    case Rating.fRAUD: return const Text("Betrug", style: TextStyle(color: Colors.white));
    case Rating.pOLL: return const Text("Umfrage", style: TextStyle(color: Colors.white));
  }
}

Color bgColor(Rating rating) {
  switch (rating) {
    case Rating.aLEGITIMATE: return const Color.fromRGBO(72, 199, 142, 1);
    case Rating.uNKNOWN: return const Color.fromRGBO(170, 172, 170, 1);
    case Rating.pING: return const Color.fromRGBO(31, 94, 220, 1);
    case Rating.pOLL: return const Color.fromRGBO(157, 31, 220, 1);
    case Rating.aDVERTISING: return const Color.fromRGBO(255, 224, 138, 1);
    case Rating.gAMBLE: return const Color.fromRGBO(241, 122, 70, 1);
    case Rating.fRAUD: return const Color.fromRGBO(241, 70, 104, 1);
  }
}

Icon icon(Rating rating) {
  switch (rating) {
    case Rating.aLEGITIMATE: return const Icon(Icons.check);
    case Rating.uNKNOWN: return const Icon(Icons.question_mark);
    case Rating.pING: return const Icon(Icons.block);
    case Rating.pOLL: return const Icon(Icons.query_stats);
    case Rating.aDVERTISING: return const Icon(Icons.ondemand_video);
    case Rating.gAMBLE: return const Icon(Icons.videogame_asset_off);
    case Rating.fRAUD: return const Icon(Icons.warning);
  }
}


class RateScreen extends StatelessWidget {
  Call call;

  RateScreen(this.call, {Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Rate ' + call.phone),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: Rating.values.map((rating) => ratingButton(context, rating, call)).toList()
        )
      ),
    );
  }

  Widget ratingButton(BuildContext context, Rating rating, Call call) {
    return Container(
      margin: const EdgeInsets.all(10),
      child: ElevatedButton(
        style: ElevatedButton.styleFrom(
          primary: bgColor(rating),
          shadowColor: Colors.blueGrey,
          elevation: 3,
          shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(32.0)),
          minimumSize: const Size(200, 60),
          maximumSize: const Size(200, 60),
        ),
        onPressed: () {
          Navigator.pop(context);
        },
        child: Row(mainAxisSize: MainAxisSize.max, children: [
          Padding(padding: const EdgeInsets.only(right: 10), child: icon(rating)),
          label(rating)
        ]),
      ),
    );
  }

}